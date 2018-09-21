import {
    compose,
    withStateHandlers,
    withProps,
    setDisplayName,
} from 'recompose'
import { withPropsLog, omitProps } from '../hocs/utils'

/**
 * Takes an array of field definitions and creates state and handlers for each
 * @param fieldDefsF A props function that returns an array of field definitions:
 * {
 *      name: String, - the name of the field. used to retrieve values
 *      defaultValue: Any, - initial value of the field
 *      validate: Any => Any - validator function. passed value to validate, returns either error(s) or null if value is valid
 * }
 *
 * @return Props in the form:
 * {
 *      fields: {
 *          [field.name]: {
 *              value: Any,
 *              onChange: Any =>
 *              error: Any,
 *              label: String
 *          }
 *      },
 *      isValid - true if there are no errors in any field. note that this doesn't actually validate any of the fields, so in its first state `isValid` will always return true
 *      validateAll - function that manually validates all the fields. this can be called when "submitting" the form to check that there aren't any invalid fields
 * }
 */
export const withFields = fieldDefsF => compose(
    setDisplayName( 'withFields' ),
    withProps( props => ( {
        fieldDefs: fieldDefsF( props ),
    } ) ),
    withStateHandlers(
        ( {
            fieldDefs
        } ) => ( {
            fields: fieldDefs.reduce( ( state, fieldDef ) => Object.assign( {}, state, {
                [ fieldDef.name ]: {
                    value: fieldDef.defaultValue,
                    error: null,
                },
            } ), {} )
        } ),
        {
            _onChangeField: ( { fields } ) => ( fieldDef, value ) => ( {
                fields: Object.assign( {}, fields, {
                    [ fieldDef.name ]: {
                        value,
                        error: fieldDef.validateOnChange !== false && fieldDef.validate ?
                            fieldDef.validate( value ) :
                            null,
                    }
                } )
            } ),
            _validateField: ( { fields } ) => fieldDef => ( {
                fields: Object.assign( {}, fields, {
                    [ fieldDef.name ]: {
                        error: fieldDef.validate && fieldDef.validate( fields[ fieldDef.name ].value )
                    }
                } )
            } ),
            _setFieldError: ( { fields } ) => ( fieldDef, error ) => ( {
                fields: Object.assign( {}, fields, {
                    [ fieldDef.name ]: Object.assign( {}, fields[ fieldDef.name ], {
                        error,
                    } )
                } )
            } )
        }
    ),
    withProps( ( {
        fields,
        fieldDefs,
        _onChangeField,
        _validateField,
        _setFieldError,
    } ) => ( {
        fields: Object.assign( {}, fields, fieldDefs.reduce( ( acc, fieldDef ) => Object.assign( {}, acc, {
            [ fieldDef.name ]: Object.assign( {}, fields[ fieldDef.name ], {
                label: fieldDef.label,
                options: fieldDef.options,
                onChange: val => _onChangeField( fieldDef, val ),
                validate: () => _validateField( fieldDef ),
                setError: err => _setFieldError( fieldDef, err ),
            } )
        } ), {} ) )
    } ) ),
    withProps( ( {
        fields,
        fieldDefs,
    } ) => ( {
        isValid: fieldDefs.reduce( ( isValid, fieldDef ) => isValid && !fields[ fieldDef.name ].error, true ),
        validateAll: () => fieldDefs.reduce( ( isValid, fieldDef ) => {
            const error = fieldDef.validate && fieldDef.validate( fields[ fieldDef.name ].value )
            fields[ fieldDef.name ].setError( error )
            return isValid && !error
        }, true )
    } ) ),
    omitProps( '_onChangeField', '_validateField', '_onSetFieldError' ),
    withPropsLog( false ),
)

/**
 * Validates that the given field is truthy. Otherwise sets the given error message
 * @param notPresentMessage
 * @returns {function(*=, *)}
 */
export const isTruthy = ( notPresentMessage ) => v => {
    if ( !v ) {
        return [ notPresentMessage ]
    }
    return null
}

/**
 * Validates that the value has at least `length` characters
 * @param length
 * @param notLongEnoughMessage
 * @returns {function(*)}
 */
export const minLength = ( length, notLongEnoughMessage ) => v => {
    if ( v.length < length ) {
        return [ notLongEnoughMessage ]
    }
    return null
}

/**
 * Combine all the validators such that it will accumulate all the error messages
 * @param fs Array of validator functions
 * @returns {function(*=)}
 */
export const accumulateErrors = fs => v => fs.reduce( ( a, b ) => {
    const err = b( v )
    if ( err ) {
        if ( a === null ) {
            return err
        }
        return [ ...a, ...err ]
    }
    return a
}, null )

/**
 * Combine the given validators such that only the first error is returned
 * @param fs
 * @returns {function(*=)}
 */
export const firstError = fs => v => fs.reduce( ( a, b ) => {
    if ( a === null ) {
        return b( v )
    }
    return a
}, null )
