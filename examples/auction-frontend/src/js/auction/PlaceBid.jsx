import React from 'react'
import {compose, withHandlers, withProps} from 'recompose'
import {Button} from 'react-bootstrap'
import {isTruthy, withFields} from '../hocs/form'
import {withPropsLog} from '../hocs/utils'
import {placeBid as placeBidApi} from '../api/auctionApi'
import FieldGroup from '../components/FieldGroup.jsx'
import randomUuid from "../utils/uuid";
import {withRouter} from "react-router";

const PlaceBid = ( { fields, onSubmit } ) => (
    <form>
        <h4>Place bid</h4>
        <FieldGroup
            type="number"
            placeholder="Enter amount to bid"
            field={ fields.amount }
            onChange={ ( x ) => fields.amount.onChange( x.target.value ) }
        />
        <Button type="submit" onClick={ onSubmit }>Submit</Button>
    </form>
)

const placeBid = ( { history } ) => ( auctionId, accountId, amount ) => {
    const reservationId = randomUuid()
    placeBidApi( { auctionId, reservationId, accountId, amount } ).then( () => {
        // console.log( result )
        window.location.reload()
        // history.push( routes.auctionDetail.replace( ':id', accountId ).replace( ':auctionId', auctionId ) )
    } ).catch( ( { message } ) => {
        alert( `Error placing bid\n\n${message}` )
    } )
}

export default compose(
    withRouter,
    withProps(({match}) => ({
        accountId: match && match.params && match.params.id,
        auctionId: match && match.params && match.params.auctionId
    })),
    withFields( ( ) => ( [
        {
            name: 'amount',
            label: 'Amount',
            validate: isTruthy( 'Please enter amount of funds to add' ),
        }
    ] ) ),
    withHandlers( {
        onSubmit: ( {
            fields,
            validateAll,
            history,
            auctionId,
            accountId
        } ) => ( e ) => {
            e.preventDefault()
            if ( validateAll() ) {
                // console.log( fields )
                placeBid( { history } )( auctionId, accountId, fields.amount.value )
            }
            else {
                // console.log( fields )
            }
        }

    } ),
    withPropsLog( false )
)( PlaceBid )
