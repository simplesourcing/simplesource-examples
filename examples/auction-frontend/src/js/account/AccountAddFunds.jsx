import React from 'react'
import { compose, withHandlers, withProps } from 'recompose'
import { Button } from 'react-bootstrap'
import { isTruthy, withFields } from '../hocs/form'
import { withPropsLog } from '../hocs/utils'
import { addFunds as addFundsApi } from '../api/auctionApi'
import routes from '../enum/routes'
import FieldGroup from '../components/FieldGroup.jsx'
import withSingleAccountDetail from './singleAccountDetail'
import AccountInfo from './AccountInfo.jsx'

const AccountAddFunds = ( { fields, onSubmit, account } ) => (
    <div>
        <h3>Add Funds</h3>
        <AccountInfo account={ account } />
        <form>
            <FieldGroup
                type="number"
                placeholder="Enter amount of funds to add"
                field={ fields.amount }
                onChange={ ( x ) => fields.amount.onChange( x.target.value ) }
            />
            <Button type="submit" onClick={ onSubmit }>Submit</Button>
        </form>
    </div>
)

const addFunds = ( { history } ) => ( accountId, amount ) => {
    addFundsApi( { accountId, amount } ).then( () => {
        // console.log( result )
        history.push( routes.accountDetail.replace( ':id', accountId ) )
    } ).catch( ( { message } ) => {
        alert( `Error adding funds\n\n${message}` )
    } )
}

export default compose(
    withSingleAccountDetail(),
    withProps( ( { match } ) => ( {
        accountId: match && match.params && match.params.id
    } ) ),
    withFields( ( ) => ( [
        {
            name: 'amount',
            label: 'Amount',
            validate: isTruthy( 'Please enter amount of funds to add' ),
        }
    ] ) ),
    withHandlers( {
        onSubmit: ( {
            fields,
            validateAll,
            history,
            accountId
        } ) => ( e ) => {
            e.preventDefault()
            if ( validateAll() ) {
                // console.log( fields )
                addFunds( { history } )( accountId, fields.amount.value )
            }
            else {
                // console.log( fields )
            }
        }

    } ),
    withPropsLog( false )
)( AccountAddFunds )
