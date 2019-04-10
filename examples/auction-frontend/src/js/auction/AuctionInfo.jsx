import React from 'react'
import { Col, Grid, Row } from 'react-bootstrap'

const AuctionInfo = ( { auction: { title, description, start, duration, price, reservePrice, bids, status, winner, id }, account } ) => (
    <Grid>
        <Row>
            <Col xs={ 2 }>Title</Col>
            <Col xs={ 3 }>{ title }</Col>
        </Row>
        <Row>
            <Col xs={ 2 }>Description</Col>
            <Col xs={ 3 }>{ description }</Col>
        </Row>
        <Row>
            <Col xs={ 2 }>Start</Col>
            <Col xs={ 3 }>{ new Date(start).toLocaleString() }</Col>
        </Row>
        <Row>
            <Col xs={ 2 }>Duration</Col>
            <Col xs={ 3 }>{ duration / 60000 } minutes</Col>
        </Row>
        <Row>
            <Col xs={ 2 }>Reserve Price</Col>
            <Col xs={ 3 }>{ reservePrice }</Col>
        </Row>
        <Row>
            <Col xs={ 2 }>Price</Col>
            <Col xs={ 3 }>{ price }</Col>
        </Row>
        <Row>
            <Col xs={ 2 }>Bids</Col>
            <Col xs={ 3 }>{ bids.length }</Col>
        </Row>
        <Row>
            <Col xs={ 2 }>Status</Col>
            <Col xs={ 3 }>{ status }</Col>
        </Row>
        {
            winner &&
            <Row>
                <Col xs={ 2 }>Winner</Col>
                <Col xs={ 3 }>{ account.id === winner ? "YOU WIN!" : "Not you!" }</Col>
            </Row>
        }
    </Grid>
)

export default AuctionInfo
