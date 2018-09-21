import { compose, withProps } from 'recompose'
import { withRouter } from 'react-router'
import showContentIfDefined from '../hocs/showContentIfDefined.jsx'
import fetchToProp from '../hocs/fetchToProp'
import { getAccountDetail } from '../api/auctionApi'
import { withPropsLog } from '../hocs/utils'

export default ( refreshes ) => compose(
    withRouter,
    withProps( ( { match } ) => ( {
        accountId: match && match.params && match.params.id
    } ) ),
    showContentIfDefined( [ 'accountId' ] ),
    fetchToProp( 'account', 'accountFetchError', refreshes || 1, 1000 )( getAccountDetail ),
    showContentIfDefined( [ 'account' ] ),
    withPropsLog( false )
)
