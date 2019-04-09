import React from 'react'
import { compose, withHandlers, withProps } from 'recompose'
import { Button } from 'react-bootstrap'
import { isTruthy, withFields } from '../hocs/form'
import { withPropsLog } from '../hocs/utils'
import { createAuction as createAuctionApi } from '../api/auctionApi'
import routes from '../enum/routes'
import FieldGroup from '../components/FieldGroup.jsx'
import withSingleAccountDetail from '../account/singleAccountDetail'
import randomUuid from "../utils/uuid";

const AuctionCreate = ({ fields, onSubmit, account } ) => (
    <div>
        <h3>Create auction</h3>
        <form>
            <FieldGroup
                type="text"
                placeholder="Title"
                field={ fields.title }
                onChange={ ( x ) => fields.title.onChange( x.target.value ) }
            />
            <FieldGroup
                type="text"
                placeholder="Description"
                field={ fields.description }
                onChange={ ( x ) => fields.description.onChange( x.target.value ) }
            />
            <FieldGroup
                type="number"
                placeholder="Reserve Price"
                field={ fields.reservePrice }
                onChange={ ( x ) => fields.reservePrice.onChange( x.target.value ) }
            />
            <FieldGroup
                type="number"
                placeholder="Duration in minutes"
                field={ fields.duration }
                onChange={ ( x ) => fields.duration.onChange( x.target.value ) }
            />
            <Button type="submit" onClick={ onSubmit }>Submit</Button>
        </form>
    </div>
)

const createAuction = ( { history } ) => ( accountId, title, description, reservePrice, duration ) => {
    const auctionId = randomUuid()
    createAuctionApi( { auctionId, accountId, title, description, reservePrice, duration } ).then( () => {
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
            name: 'title',
            label: 'Title',
            validate: isTruthy( 'Please enter a title for your auction' ),
        },
        {
            name: 'description',
            label: 'Description',
            validate: isTruthy( 'Please enter a description for your auction' ),
        },
        {
            name: 'reservePrice',
            label: 'Reserve price',
            validate: isTruthy( 'Please enter a reserve price' ),
        },
        {
            name: 'duration',
            label: 'Duration',
            validate: isTruthy( 'Please enter a duration' ),
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
                createAuction( { history } )( accountId, fields.title.value, fields.description.value,
                    fields.reservePrice.value, fields.duration.value * 60000 )
            }
            else {
                // console.log( fields )
            }
        }

    } ),
    withPropsLog( false )
)( AuctionCreate )
