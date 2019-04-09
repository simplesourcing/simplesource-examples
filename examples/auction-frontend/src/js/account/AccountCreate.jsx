import React from 'react'
import { compose, withHandlers } from 'recompose'
import { withRouter } from 'react-router'
import { isTruthy, withFields } from '../hocs/form'
import { withPropsLog } from '../hocs/utils'
import randomUuid from '../utils/uuid'
import { createAccount } from '../api/auctionApi'
import FieldGroup from '../components/FieldGroup.jsx'
import routes from "../enum/routes";
import {Link} from "react-router-dom";

const AccountCreate = ( { fields, onSubmit } ) => (
    <div>
        <h3>Create Account</h3>
        <form>
            <FieldGroup
                type="text"
                placeholder="Enter user name"
                field={ fields.userName }
                onChange={ ( x ) => fields.userName.onChange( x.target.value ) }
            />
            <FieldGroup
                type="number"
                placeholder="Enter funds"
                field={ fields.funds }
                onChange={ ( x ) => fields.funds.onChange( x.target.value ) }
            />
            <button type="button" className="btn btn-default btn-lg" onClick={ onSubmit }>Submit</button>
            <Link to={ routes.accounts }>Back</Link>
        </form>
    </div>
)

const addAccount = ( history ) => ( userName, funds ) => {
    const accountId = randomUuid()
    createAccount( { userName, funds, accountId } ).then( () => {
        history.push( '/accounts' )
    } ).catch( ( { message } ) => {
        alert( `Error creating account\n\n${message}` )
    } )
}

export default compose(
    withRouter,
    withFields( ( ) => ( [
        {
            name: 'userName',
            label: 'User Name',
            validate: isTruthy( 'Please provide a user name' ),

        },
        {
            name: 'funds',
            label: 'Funds',
            validate: isTruthy( 'Please enter initial funds' ),
        }
    ] ) ),
    withHandlers( {
        onSubmit: ( {
            fields,
            validateAll,
            history
        } ) => ( e ) => {
            e.preventDefault()
            if ( validateAll() ) {
                addAccount( history )( fields.userName.value, fields.funds.value )
            }
        }

    } ),
    withPropsLog( false )
)( AccountCreate )
