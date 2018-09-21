import React from 'react'
import { compose, withHandlers, withProps } from 'recompose'
import { Button } from 'react-bootstrap'
import { isTruthy, withFields } from '../hocs/form'
import { withPropsLog } from '../hocs/utils'
import { confirmReservation as confirmReservationApi } from '../api/auctionApi'
import routes from '../enum/routes'
import FieldGroup from '../components/FieldGroup.jsx'
import withSingleAccountDetail from './singleAccountDetail'
import AccountInfo from './AccountInfo.jsx'

const ReservationConfirm = ( { fields, onSubmit, account, reservation: { description, amount } } ) => (
    <div>
        <h3>Confirm Reservation: { description }</h3>
        <AccountInfo account={ account } />
        <form>
            <FieldGroup
                type="number"
                placeholder="Enter amount of funds to confirm"
                defaultValue={ amount }
                field={ fields.amount }
                onChange={ ( x ) => fields.amount.onChange( x.target.value ) }
            />
            <Button type="submit" onClick={ onSubmit }>Submit</Button>
        </form>
    </div>
)

const confirmReservation = ( history ) => ( accountId, reservationId, amount ) => {
    confirmReservationApi( { accountId, reservationId, amount } ).then( () => {
        history.push( routes.accountDetail.replace( ':id', accountId ) )
    } ).catch( ( { message } ) => {
        alert( `Error confirming reservation\n\n${message}` )
    } )
}

export default compose(
    withSingleAccountDetail(),
    withProps( ( { match } ) => ( {
        reservationId: match && match.params && match.params.resId
    } ) ),
    withProps( ( { reservationId, account: { draftReservations } } ) => ( {
        reservation: draftReservations &&
            draftReservations.find( res => res.reservationId === reservationId )
    } ) ),
    withFields( ( { reservation: { amount } } ) => ( [
        {
            name: 'amount',
            label: 'Amount',
            defaultValue: amount,
            validate: isTruthy( 'Please enter amount of funds to confirm' ),
        }
    ] ) ),
    withHandlers( {
        onSubmit: ( {
            fields,
            validateAll,
            history,
            accountId,
            reservationId
        } ) => ( e ) => {
            e.preventDefault()
            if ( validateAll() ) {
                confirmReservation( history )( accountId, reservationId, fields.amount.value )
            }
        }
    } ),
    withPropsLog( false ),
)( ReservationConfirm )
