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
import { getAccountTransactions } from '../api/auctionApi'
import TransactionList from './TransactionList.jsx'

const AccountDetail = ( {
    account: { userName, funds, availableFunds, id, draftReservations: reservations },
    transactions, transactionsFetchError, userEditable, startUserEdit, endUserEdit, reserveFunds, addFunds, refreshFetch
} ) => (
    <div>
        <Link to={ routes.accounts }><h3>Accounts</h3></Link>
        <Grid>
            {userEditable ?
                <UserNameDetailEdit initialUserName={ userName } accountId={ id } endEdit={ endUserEdit } /> :
                <UserNameDetailDisplay userName={ userName } startEdit={ startUserEdit } />}
            <Row>
                <Col xs={ 3 }>Funds</Col>
                <Col xs={ 3 }>{funds}</Col>
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
        <Button onClick={ addFunds }>Add Funds</Button><Button onClick={ reserveFunds }>Reserve Funds</Button>
    </div> )

export default compose(
    withSingleAccountDetail( 3 ),
    renameProp( 'refreshFetch', 'refreshDetails' ),
    withState( 'userEditable', 'setUserEditable', false ),
    fetchToProp( 'transactions', 'transactionsFetchError', 2, 1000 )( getAccountTransactions ),
    renameProp( 'refreshFetch', 'refreshTransactions' ),
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
        refreshFetch: ( { refreshTransactions, refreshDetails } ) => () => {
            refreshDetails()
            refreshTransactions()
        }
    } ),
    omitProps( [ 'refreshDetails', 'refreshTransactions' ] ),
    withPropsLog( false )
)( AccountDetail )
