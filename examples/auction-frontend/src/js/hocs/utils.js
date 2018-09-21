import { withProps, setDisplayName, compose, withState, withHandlers, mapProps } from 'recompose'
import withLifeCycle from '@hocs/with-lifecycle'
import withLog from '@hocs/with-log'
import hocOmitProps from '@hocs/omit-props'
import { updateObject } from '../utils/immutable'
import { collect } from '../utils/array'

export const identity = withProps( {} )

export function withPropsLog( enable, displayName ) {
// eslint-disable-next-line no-nested-ternary
    return enable ?
        ( displayName ? compose( setDisplayName( displayName ), withLog( props => props ) ) :
            withLog( props => props ) ) :
        identity
}

export function withReceiveProps( propsFunc ) {
    return withLifeCycle( {
        onWillMount( props ) {
            propsFunc( props, undefined )
        },
        onWillUpdate( props, nextProps ) {
            propsFunc( nextProps, props )
        }
    } )
}

export function omitProps( ...propNames ) {
    return hocOmitProps( ...propNames )
}

export function keepProps( ...propNames ) {
    return mapProps( props => {
        const xxx = collect( propNames, ( name ) => {
            const value = props[ name ]
            return ( value !== undefined ) ? { [ name ]: value } : undefined
        } )
        return Object.assign( {}, ...xxx )
    } )
}

export function withUpdateableState( stateName, defaultState, ignoreFunction ) {
    const firstLetter = stateName.lastIndexOf( '_' ) + 1
    const prefix = stateName.substring( 0, firstLetter )
    const baseName = stateName.charAt( firstLetter ).toUpperCase() + stateName.substring( firstLetter + 1 )
    const setName = `${prefix}set${baseName}`
    const updateName = `${prefix}update${baseName}`
    return compose(
        withState( stateName, setName, defaultState ),
        withHandlers( {
            [ updateName ]: ( props ) => {
                const stateValue = props[ stateName ]
                const setHandler = props[ setName ]
                return ( newState ) => {
                    if ( ignoreFunction && ignoreFunction( stateValue ) ) { return }
                    const updatedState = updateObject( stateValue, newState )
                    if ( updatedState !== stateValue ) {
                        setHandler( updatedState )
                    }
                }
            }
        } )
    )
}

