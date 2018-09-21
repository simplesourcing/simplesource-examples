import React from 'react'
import { compose, withHandlers, withState } from 'recompose'
import {  Row, Col, Button, FormControl } from 'react-bootstrap'
import { updateUserName } from '../api/auctionApi'

export const UserNameDetailDisplay = ( { userName, startEdit } ) => (
    <Row>
        <Col xs={ 3 }>User Name</Col>
        <Col xs={ 3 }>{ userName }</Col>
        <Col xs={ 3 }>{ <Button onClick={ startEdit }>Edit</Button> }</Col>
    </Row>
)

const UserNameDetailEditView = ( { userName, onChange, onConfirm, onCancel } ) =>  (
    <Row>
        <Col xs={ 3 }>User Name</Col>
        <Col xs={ 3 }>
            <FormControl
                id="userName"
                type="text"
                label="User Name"
                placeholder="Enter user name"
                value={ userName }
                onChange={ ( x ) => onChange( x.target.value ) }
            />
        </Col>
        <Col xs={ 3 }><Button onClick={ onConfirm }>Save</Button><Button onClick={ onCancel }>Cancel</Button></Col>
    </Row>
)

export const UserNameDetailEdit = compose(
    withState( 'userName', 'setUserName', ( { initialUserName } ) => initialUserName ),
    withHandlers( {
        onChange: ( { setUserName } ) => ( u ) => { setUserName( u ) },
        onConfirm: ( { userName, endEdit, accountId, initialUserName } ) => ( e ) => {
            e.preventDefault()
            if ( userName !== initialUserName ) {
                updateUserName( { accountId, userName } ).catch( ( { message } ) => {
                    alert( `Error editing user name\n\n${message}` )
                } )
                endEdit( true )
            }
        },
        onCancel: ( { endEdit } ) => ( e ) => {
            e.preventDefault()
            endEdit( false )
        },
    } )
)( UserNameDetailEditView )
