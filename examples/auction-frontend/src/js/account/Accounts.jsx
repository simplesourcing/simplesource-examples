import React from 'react'
import { Link, withRouter } from 'react-router-dom'
import { Button } from 'react-bootstrap'
import { compose, withState, withHandlers } from 'recompose'
import { withPropsLog } from '../hocs/utils'
import { getAccounts } from '../api/auctionApi'
import routes from '../enum/routes'
import fetchToProp from '../hocs/fetchToProp'
import '../../css/font-awesome.css'

const AccountInfo = ( { userName, funds, availableFunds, draftReservations, id } ) => ( <tr>
    <td><Link to={ `${routes.accounts}/${id}` }>{ userName }</Link></td>
    <td>{ funds }</td>
    <td>{ availableFunds }</td>
    <td>{ draftReservations && draftReservations.length }</td>
</tr> )

const Accounts = ( { accounts, accountsFetchError, onCreate } ) => (
    <div>
        <h3>Accounts</h3>
        { accountsFetchError && <div><h4>Error fetching accounts:</h4>{ accountsFetchError }</div>}
        { accounts && <div>
            <table className="table table-striped table-hover">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Funds</th>
                        <th>Available</th>
                        <th>Reservations</th>
                    </tr>
                </thead>
                <tbody>{
                    accounts.map( acc => (
                        <AccountInfo key={ acc.accountId } { ...acc } />
                    ) )
                }
                </tbody>
            </table>
        </div>
        }
        <div>
            <Button onClick={ onCreate }>Create New Account</Button>
        </div>
    </div>
)

export default compose(
    withRouter,
    withState( 'accounts', 'setAccounts', null ),
    fetchToProp( 'accounts', 'accountsFetchError', 2, 1000 )( getAccounts ),
    withHandlers( {
        onCreate: ( { history } ) => () => history.push( routes.accountCreate )
    } ),
    withPropsLog( false )
)( Accounts )
