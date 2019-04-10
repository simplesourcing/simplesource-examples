import { compose, withProps } from 'recompose'
import { withRouter } from 'react-router'
import showContentIfDefined from '../hocs/showContentIfDefined.jsx'
import fetchToProp from '../hocs/fetchToProp'
import { getAuctionDetail } from '../api/auctionApi'
import { withPropsLog } from '../hocs/utils'

export default ( refreshes ) => compose(
    withRouter,
    withProps( ( { match } ) => ( {
        auctionId: match && match.params && match.params.id
    } ) ),
    showContentIfDefined( [ 'auctionId' ] ),
    fetchToProp( 'auction', 'auctionFetchError', refreshes || 1, 1000 )( getAuctionDetail ),
    showContentIfDefined( [ 'auction' ] ),
    withPropsLog( false )
)
