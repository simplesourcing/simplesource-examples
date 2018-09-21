import React from 'react'
import { compose, withHandlers, withProps } from 'recompose'
import { Button } from 'react-bootstrap'
import { isTruthy, withFields } from '../hocs/form'
import { withPropsLog } from '../hocs/utils'
import randomUuid from '../utils/uuid'
import { reserveFunds as reserveFundsApi } from '../api/auctionApi'
import routes from '../enum/routes'
import FieldGroup from '../components/FieldGroup.jsx'
import withSingleAccountDetail from './singleAccountDetail'
import AccountInfo from './AccountInfo.jsx'

const AccountReserveFunds = ( { fields, onSubmit, account } ) => (
    <div>
        <h3>Reserve Funds</h3>
        <AccountInfo account={ account } />
        <form>
            <FieldGroup
                type="text"
                placeholder="Enter description for the reservation"
                field={ fields.description }
                onChange={ ( x ) => fields.description.onChange( x.target.value ) }
            />
            <FieldGroup
                type="number"
                placeholder="Enter amount to reserve"
                field={ fields.amount }
                onChange={ ( x ) => fields.amount.onChange( x.target.value ) }
            />
            <Button type="submit" onClick={ onSubmit }>Submit</Button>
        </form>
    </div>
)

const reserveFunds = ( history ) => ( accountId, description, amount ) => {
    const reservationId = randomUuid()
    reserveFundsApi( { accountId, reservationId, description, amount } ).then( () => {
        history.push( routes.accountDetail.replace( ':id', accountId ) )
    } ).catch( ( { message } ) => {
        alert( `Error reserving funds\n\n${message}` )
    } )
}

export default compose(
    withSingleAccountDetail(),
    withProps( ( { match } ) => ( {
        accountId: match && match.params && match.params.id
    } ) ),
    withFields( ( ) => ( [
        {
            name: 'description',
            label: 'Reservation description',
            validate: isTruthy( 'Please provide a description' ),

        },
        {
            name: 'amount',
            label: 'Amount',
            validate: isTruthy( 'Please enter amount to reserve' ),
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
                reserveFunds( history )( accountId, fields.description.value, fields.amount.value )
            }
        }

    } ),
    withPropsLog( false )
)( AccountReserveFunds )
