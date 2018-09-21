import React from 'react'
import { compose, withHandlers } from 'recompose'
import { Grid, Row, Col, Button } from 'react-bootstrap'
import { withRouter } from 'react-router'
import { cancelReservation as cancelReservationApi } from '../api/auctionApi'
import routes from '../enum/routes'

const ReservationList = ( { reservations, onConfirm, onCancel } ) => (
    <div>
        <Grid>
            { reservations.map( res => (
                <Row key={ res.reservationId }>
                    <Col xs={ 3 }>{ res.description }</Col>
                    <Col xs={ 3 }>{ res.amount }</Col>
                    <Col xs={ 3 }>
                        <Button onClick={ () => onConfirm( res.reservationId  ) }>Confirm</Button>
                        <Button onClick={ () => onCancel( res.reservationId  ) }>Cancel</Button>
                    </Col>
                </Row>
            ) ) }
        </Grid>
    </div>
)

const confirmReservation = ( history, accountId, resId ) => {
    history.push( routes.reservationConfirm.replace( ':id', accountId ).replace( ':resId', resId ) )
}

const cancelReservation = ( refreshAccount, accountId, resId ) => {
    cancelReservationApi( { accountId, reservationId: resId } ).catch( ( { message } ) => {
        alert( `Error cancelling reservation\n\n${message}` )
    } )
    refreshAccount()
}

export default compose(
    withRouter,
    withHandlers( {
        onConfirm: ( { accountId, history } ) => resId =>
            confirmReservation( history, accountId, resId ),
        onCancel: ( { accountId, refreshAccount } ) => resId =>
            cancelReservation( refreshAccount, accountId, resId ),
    } )
)( ReservationList )
