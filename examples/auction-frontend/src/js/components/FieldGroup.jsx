import React from 'react'
import { ControlLabel, FormControl, FormGroup, HelpBlock } from 'react-bootstrap'

function FieldGroup( { field, ...props } ) {
    return (
        <FormGroup controlId={ field.name } validationState={ field.error && field.error[ 0 ] && 'error' }>
            <ControlLabel>{ field.label }</ControlLabel>
            <FormControl { ...props } />
            {field.error && <HelpBlock>{field.error[ 0 ]}</HelpBlock>}
        </FormGroup>
    )
}

export default FieldGroup
