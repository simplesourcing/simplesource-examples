import {
    API_ACCOUNTS,
    API_CREATE_ACCOUNT,
    API_UPDATE_ACCOUNT,
    API_RESERVE_FUNDS,
    API_ADD_FUNDS,
    API_CANCEL_RESERVATION,
    API_CONFIRM_RESERVATION,
    API_ACCOUNT_TRANSACTIONS,
    API_AUCTIONS,
    API_CREATE_AUCTION,
    API_START_AUCTION,
    API_COMPLETE_AUCTION,
    API_PLACE_BID,
} from '../settings/api'
import { sendGet, sendPost, sendPut, sendDelete } from './utils'

export const getAccounts = ( ) => sendGet( API_ACCOUNTS( ) ).then( result => {
    const { _embedded } = result
    return _embedded.accounts
} )

export const getAccountDetail = ( { accountId } ) => sendGet( API_ACCOUNTS( ) ).then( result => {
    const { _embedded } = result
    const accounts = _embedded.accounts
    return accounts.find( acc => acc.id === accountId )
} )

export const createAccount = ( { accountId, userName, funds } ) =>
    sendPost( API_CREATE_ACCOUNT( ), {
        accountId,
        accountDto: {
            userName,
            funds
        }
    } )

export const reserveFunds = ( { accountId, reservationId, description, amount } ) =>
    sendPost( API_RESERVE_FUNDS( accountId ), {
        reservationId,
        description,
        amount
    } )

export const cancelReservation = ( { accountId, reservationId } ) =>
    sendDelete( API_CANCEL_RESERVATION( accountId, reservationId ) )

export const confirmReservation = ( { accountId, reservationId, amount } ) =>
    sendPost( API_CONFIRM_RESERVATION( accountId, reservationId ), {
        amount
    } )

export const addFunds = ( { accountId, amount } ) =>
    sendPost( API_ADD_FUNDS( accountId ), {
        funds: amount
    } )

export const updateUserName = ( { accountId, userName } ) =>
    sendPut( API_UPDATE_ACCOUNT( accountId ), {
        userName,
    } )

export const getAccountTransactions = ( { accountId } ) =>
    sendGet( API_ACCOUNT_TRANSACTIONS( accountId ) ).then( result => result )

export const getAuctions = ( ) => sendGet( API_AUCTIONS( ) ).then( result => {
    const { _embedded } = result
    return _embedded.auctions
} )

export const getAuctionDetail = ( { auctionId } ) => sendGet( API_AUCTIONS( ) ).then( result => {
    const { _embedded } = result
    const auctions = _embedded.auctions
    return auctions.find( auc => auc.id === auctionId )
} )

export const createAuction = ( { auctionId, accountId, title, description, reservePrice, duration } ) =>
    sendPost( API_CREATE_AUCTION( ), {
        auctionId,
        auctionDto: {
            creator: accountId,
            title,
            description,
            reservePrice,
            duration
        }
    } )


export const startAuction = ( { auctionId } ) =>
    sendPost( API_START_AUCTION( auctionId ), {
    } )

export const completeAuction = ( { auctionId } ) =>
    sendPost( API_COMPLETE_AUCTION( auctionId ), {
    } )

export const placeBid = ( { auctionId, reservationId, accountId, amount } ) =>
    sendPost( API_PLACE_BID( auctionId ), {
        reservationId,
        accountId,
        amount
    } )