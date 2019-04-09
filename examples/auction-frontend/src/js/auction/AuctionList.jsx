import React from 'react'
import {Link} from "react-router-dom";
import routes from "../enum/routes";
import { Button } from 'react-bootstrap'
import {startAuction as startAuctionApi, completeAuction as completeAuctionApi} from "../api/auctionApi";

const getTimeRemaining = (start, duration) => {
    let now = new Date();
    let endTimeMillis = start + duration;
    let secondsRemaining = (endTimeMillis - now.valueOf()) / 1000;
    if (secondsRemaining <= 0) {
        return "0";
    } else if (secondsRemaining <= 60) {
        return "< 1 minute";
    } else {
        return Math.round(secondsRemaining / 60) + " minutes";
    }
}

const isAuctionLive = (start, duration) => {
    let now = new Date();
    let endTimeMillis = start + duration;
    return now.valueOf() < endTimeMillis;
}

const startAuction = (auctionId) => {
    startAuctionApi( { auctionId } ).then( () => {
        // console.log( result )
        window.location.reload()
    } ).catch( ( { message } ) => {
        alert( `Error starting auction\n\n${message}` )
    } )
}

const completeAuction = (auctionId) => {
    completeAuctionApi( { auctionId } ).then( () => {
        // console.log( result )
        window.location.reload()
    } ).catch( ( { message } ) => {
        alert( `Error completing auction\n\n${message}` )
    } )
}

const AuctionInfo = ( { title, creator, description, bids, price, status, start, duration, winner, id, accountId } ) => ( <tr>
    <td><Link to={ `${routes.accounts}/${accountId}/auctions/${id}` }>{ title }</Link></td>
    <td>{ description }</td>
    <td>{ price }</td>
    <td>{ bids.length }</td>
    <td>{ Math.round(duration / 60000) } minutes</td>
    <td>{ (status === 'STARTED') ? getTimeRemaining(start, duration) : "-" }</td>
    <td>{ status }</td>
    <td>{ status === 'CREATED' && accountId === creator && <Button onClick={ () => startAuction(id) }>Start auction</Button> }
        { status === 'STARTED' && isAuctionLive(start, duration) && <Link to={ `${routes.accounts}/${accountId}/auctions/${id}` }>Place bid</Link> }
        { status === 'STARTED' && !isAuctionLive(start, duration) && creator === accountId && <Button onClick={ () => completeAuction(id) }>Complete auction</Button> }
        { winner === accountId && "You win!" }
    </td>
</tr> )

const AuctionList = ( { auctions, account } ) => (
    <table className="table table-striped table-hover">
        <thead>
        <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Price</th>
            <th>Bids</th>
            <th>Duration</th>
            <th>Time remaining</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>{
            auctions.map( auc => (
                <AuctionInfo key={ auc.id } { ...auc } accountId={ account } />
            ) )
        }
        </tbody>
    </table>
)

export default AuctionList
