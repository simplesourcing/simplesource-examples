import {
    API_ACCOUNTS,
    API_CREATE_ACCOUNT,
    API_UPDATE_ACCOUNT,
    API_RESERVE_FUNDS,
    API_ADD_FUNDS,
    API_CANCEL_RESERVATION,
    API_CONFIRM_RESERVATION,
    API_ACCOUNT_TRANSACTIONS
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
