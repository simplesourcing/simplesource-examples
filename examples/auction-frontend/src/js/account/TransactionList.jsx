import React from 'react'
import { Grid, Row, Col } from 'react-bootstrap'

const TransactionList = ( { transactions } ) => (
    <div>
        <Grid>
            { transactions.map( trans => (
                <Row key={ trans.reservationId }>
                    <Col xs={ 3 }>{ trans.description }</Col>
                    <Col xs={ 3 }>{ trans.amount }</Col>
                    <Col xs={ 3 }>{ trans.status }</Col>
                </Row>
            ) ) }
        </Grid>
    </div>
)

export default TransactionList
