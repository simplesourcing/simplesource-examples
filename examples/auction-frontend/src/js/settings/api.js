import { API_BASE_URL, API_BASE_URL_PROJECTION } from './main'

export const API_ACCOUNTS = () => `${API_BASE_URL_PROJECTION}/accounts`
export const API_ACCOUNT_TRANSACTIONS = ( accountId ) => `${API_BASE_URL_PROJECTION}/accounts/${accountId}/transactions`
export const API_CREATE_ACCOUNT = ( ) => `${API_BASE_URL}/accounts`
export const API_UPDATE_ACCOUNT = ( accountId ) => `${API_BASE_URL}/accounts/${accountId}`
export const API_ADD_FUNDS = ( accountId ) => `${API_BASE_URL}/accounts/${accountId}/funds`
export const API_RESERVE_FUNDS = ( accountId ) => `${API_BASE_URL}/accounts/${accountId}/funds/reservations`
export const API_CANCEL_RESERVATION = ( accountId, reservationId ) => `${API_BASE_URL}/accounts/${accountId}/funds/reservations/${reservationId}`
export const API_CONFIRM_RESERVATION = API_CANCEL_RESERVATION
export const API_AUCTIONS = () => `${API_BASE_URL_PROJECTION}/auctions`
export const API_CREATE_AUCTION = () => `${API_BASE_URL}/auctions`
export const API_START_AUCTION = ( auctionId ) => `${API_BASE_URL}/auctions/${auctionId}/start`
export const API_COMPLETE_AUCTION = ( auctionId ) => `${API_BASE_URL}/auctions/${auctionId}/complete`
export const API_PLACE_BID = ( auctionId ) => `${API_BASE_URL}/auctions/${auctionId}/bid`
