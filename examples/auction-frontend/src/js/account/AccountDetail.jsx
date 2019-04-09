import React from 'react'
import { compose, withHandlers, withState, renameProp } from 'recompose'
import { Grid, Row, Col, Button } from 'react-bootstrap'
import { omitProps, withPropsLog } from '../hocs/utils'
import { Link } from 'react-router-dom'
import routes from '../enum/routes'
import withSingleAccountDetail from './singleAccountDetail'
import { UserNameDetailDisplay, UserNameDetailEdit } from './UserNameDetail.jsx'
import ReservationList from './ReservationList.jsx'
import fetchToProp from '../hocs/fetchToProp'
import { getAccountTransactions, getAuctions } from '../api/auctionApi'
import TransactionList from './TransactionList.jsx'
import AuctionList from '../auction/AuctionList'

const AccountDetail = ( {
    account: { userName, funds, availableFunds, id, draftReservations: reservations },
    transactions, transactionsFetchError, auctions, auctionsFetchError,
    userEditable, startUserEdit, endUserEdit, reserveFunds, addFunds, refreshFetch, createAuction
} ) => (
    <div>
        <h3>Welcome {userName}</h3>
        <Grid>
            {userEditable ?
                <UserNameDetailEdit initialUserName={ userName } accountId={ id } endEdit={ endUserEdit } /> :
                <UserNameDetailDisplay userName={ userName } startEdit={ startUserEdit } />}
            <Row>
                <Col xs={ 3 }>User ID</Col>
                <Col xs={ 6 }>{id}</Col>
            </Row>
            <Row>
                <Col xs={ 3 }>Funds</Col>
                <Col xs={ 3 }>{funds}</Col>
                <Col xs={ 3 }><Button onClick={ addFunds }>Add Funds</Button></Col>
            </Row>
            <Row>
                <Col xs={ 3 }>Available</Col>
                <Col xs={ 3 }>{availableFunds}</Col>
            </Row>
        </Grid>
        { reservations && reservations.length > 0 && <div>
            <h4>Draft Reservations</h4>
            <ReservationList reservations={ reservations } refreshAccount={ refreshFetch } accountId={ id } />
        </div>
        }
        {
            transactionsFetchError && <div><h5>Error fetching transactions</h5>{ transactionsFetchError }</div>
        }
        {
            transactions && transactions.length > 0 && <div>
                <h4>Transactions</h4>
                <TransactionList transactions={ transactions } />
            </div>
        }
        <br />
        {
            auctionsFetchError && <div><h5>Error fetching auctions</h5>{ auctionsFetchError }</div>
        }
        {
            auctions && auctions.length > 0 && <div>
                <h4>Auctions</h4>
                <AuctionList auctions={ auctions } account={ id }/>
            </div>
        }
        <br />
        <Button onClick={ createAuction }>Create auction</Button>
        <br />
        <Link to={ routes.accounts }>Back</Link>
    </div> )

export default compose(
    withSingleAccountDetail( 3 ),
    renameProp( 'refreshFetch', 'refreshDetails' ),
    withState( 'userEditable', 'setUserEditable', false ),
    fetchToProp( 'transactions', 'transactionsFetchError', 2, 1000 )( getAccountTransactions ),
    renameProp( 'refreshFetch', 'refreshTransactions' ),
    fetchToProp( 'auctions', 'auctionsFetchError', 2, 1000 )( getAuctions ),
    renameProp( 'refreshFetch', 'refreshAuctions' ),
    withHandlers( {
        startUserEdit: ( { setUserEditable } ) => () => setUserEditable( true ),
        endUserEdit: ( { setUserEditable, refreshDetails } ) => ( refresh ) => {
            setUserEditable( false )
            if ( refresh ) refreshDetails()
        },
        reserveFunds: ( { history, accountId } )  => () =>
            history.push( routes.accountFundsReserve.replace( ':id', accountId ) ),
        addFunds: ( { history, accountId } )  => () =>
            history.push( routes.accountFundsAdd.replace( ':id', accountId ) ),
        createAuction: ( { history, accountId } )  => () =>
            history.push( routes.auctionCreate.replace( ':id', accountId ) ),
        refreshFetch: ( { refreshTransactions, refreshDetails, refreshAuctions } ) => () => {
            refreshDetails()
            refreshTransactions()
            refreshAuctions()
        }
    } ),
    omitProps( [ 'refreshDetails', 'refreshTransactions', 'refreshAuctions' ] ),
    withPropsLog( false )
)( AccountDetail )
