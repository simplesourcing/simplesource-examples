import React from 'react'
import { Col, Grid, Row } from 'react-bootstrap'
import routes from '../enum/routes'
import { Link } from 'react-router-dom'

const AccountInfo = ( { account: { userName, id, availableFunds, funds } } ) => (
    <Grid>
        <Row>
            <Col xs={ 2 }>Account</Col>
            <Col xs={ 3 }><Link to={ routes.accountDetail.replace( ':id', id ) }>{ userName }</Link></Col>
        </Row>
        <Row>
            <Col xs={ 2 }>Total</Col>
            <Col xs={ 3 }>{ funds }</Col>
        </Row>
        <Row>
            <Col xs={ 2 }>Available</Col>
            <Col xs={ 3 }>{ availableFunds }</Col>
        </Row>
    </Grid>
)

export default AccountInfo
