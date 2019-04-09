import React from 'react'
import {compose} from 'recompose'
import {Col, Grid, Row} from 'react-bootstrap'
import {withRouter} from 'react-router'

const ReservationList = ( { reservations } ) => (
    <div>
        <Grid>
            { reservations.map( res => (
                <Row key={ res.reservationId }>
                    <Col xs={ 3 }>{ res.description }</Col>
                    <Col xs={ 3 }>{ res.amount }</Col>
                </Row>
            ) ) }
        </Grid>
    </div>
)


export default compose(
    withRouter
)( ReservationList )
