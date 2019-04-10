import React from 'react'
import { Grid, Row, Col } from 'react-bootstrap'

const BidList = ( { bids, account } ) => (
    <div>
        <Grid>
            { bids.map( bid => (
                <Row key={ bid.reservationId }>
                    <Col xs={ 3 }>{ new Date(bid.timestamp).toLocaleString() }</Col>
                    <Col xs={ 3 }>{ bid.bidder === account.id ? "Your bid" : "Someone else" }</Col>
                    <Col xs={ 3 }>{ bid.amount }</Col>
                </Row>
            ) ) }
        </Grid>
    </div>
)

export default BidList
