import React from 'react'
import {compose, withProps} from 'recompose'
import {withPropsLog} from '../hocs/utils'
import {Link} from 'react-router-dom'
import routes from '../enum/routes'
import withSingleAccountDetail from '../account/singleAccountDetail'
import AccountInfo from "../account/AccountInfo";
import AuctionInfo from "./AuctionInfo";
import {isTruthy, withFields} from "../hocs/form";
import fetchToProp from "../hocs/fetchToProp";
import {getAuctionDetail} from "../api/auctionApi";
import {withRouter} from "react-router";
import showContentIfDefined from "../hocs/showContentIfDefined";
import BidList from "./BidList"
import PlaceBid from "./PlaceBid"

const AuctionDetail = ({fields, onSubmit, account, auction}) => (
    <div>
        <h3>{auction.title}</h3>
        <AuctionInfo auction={auction} account={account} />
        {auction.bids && auction.bids.length > 0 && <div>
                <h4>Bids</h4>
                <BidList bids={auction.bids} account={account}/>
            </div>
        }
        <hr/>
        <AccountInfo account={account}/>
        {auction.status === 'STARTED' &&
            <PlaceBid />
        }
        <Link to={ `${routes.accounts}/${account.id}` }>Back</Link>
    </div>
)


export default compose(
    withRouter,
    withProps(({match}) => ({
        accountId: match && match.params && match.params.id,
        auctionId: match && match.params && match.params.auctionId
    })),
    // withSingleAuctionDetail(),
    withSingleAccountDetail(),
    showContentIfDefined( [ 'auctionId' ] ),
    fetchToProp('auction', 'auctionFetchError', 3, 1000)(getAuctionDetail),
    showContentIfDefined( [ 'auction' ] ),
    withFields(() => ([
        {
            name: 'amount',
            label: 'Amount',
            validate: isTruthy('Please enter amount to bid'),
        }
    ])),
    withPropsLog(true)
)(AuctionDetail)
