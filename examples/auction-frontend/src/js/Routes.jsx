import React from 'react'
import {Route, withRouter} from 'react-router-dom'
import {compose, withProps} from 'recompose'
import Accounts from './account/Accounts.jsx'
import AccountDetail from './account/AccountDetail.jsx'
import AccountCreate from './account/AccountCreate.jsx'
import AccountAddFunds from './account/AccountAddFunds.jsx'
import AuctionDetail from './auction/AuctionDetail.jsx'
import routes from './enum/routes'
import {withPropsLog} from './hocs/utils'
import AuctionCreate from "./auction/AuctionCreate";

const RoutesView = () => (
    <div>
        <Route exact path={ routes.Home } component={ Accounts } />
        <Route exact path={ routes.accounts } component={ Accounts } />
        <Route exact path={ routes.accountCreate } component={ AccountCreate } />
        <Route exact path={ routes.accountDetail } component={ AccountDetail } />
        <Route exact path={ routes.accountFundsAdd } component={ AccountAddFunds } />
        <Route exact path={ routes.auctionCreate } component={ AuctionCreate } />
        <Route exact path={ routes.auctionDetail } component={ AuctionDetail } />
    </div>
)

const Routes = compose(
    withRouter,
    withProps( ( { match } ) => ( { activeHref: match && match.location && match.location.pathname } ) ),
    withPropsLog( false )
)( RoutesView )

export default Routes
