import { compose, withHandlers, withProps } from 'recompose'
import { omitProps, withReceiveProps, withUpdateableState } from './utils'
import delay  from '../utils/delay'

/**
 * Calls a function that returns a promise, and sets a prop to the return value of the promise
 * @param targetPropName The name of the target prop
 * @param refreshCount Number of times to refresh the fetch
 * @param refreshInterval Interval in ms between fetches
 *
 * @param fetchFunction A function that takes props as arguments and returns a promise
 * This function would typically be a API client function that calls a rest endpoint asynchronously
 *
 * @return function (a HOC to be precise)
 */
export default ( targetPropName, errorPropName, refreshCount, refreshInterval ) => fetchFunction => compose(
    withUpdateableState( '__state', {
        [ targetPropName ]: null,
        [ errorPropName ]: null,
        refreshIndex: refreshCount || 1,
        refreshable: true
    } ),
    // TODO: this doesn't work - find something that does...
    // withLifeCycle( {
    //     onWillUnMount( { __setState } ) {
    //         __setState( {
    //             refreshIndex: 0,
    //             refreshable: false
    //         } )
    //     }
    // } ),
    withReceiveProps( ( {
        __state: { refreshable, refreshIndex },
        __updateState,
        ...props
    } ) => {
        if ( refreshable ) {
            fetchFunction( props )
                .then( result => __updateState( {
                    [ targetPropName ]: result,
                    refreshable: false
                } ), ( { message } ) => {
                    __updateState( {
                        refreshable: false,
                        refreshIndex: 0,
                        [ errorPropName ]: message
                    } )
                } )
                .then( () => delay( refreshInterval ) )
                .then( () => ( ( refreshIndex > 1 ) ?
                    __updateState( {
                        refreshIndex: refreshIndex - 1,
                        refreshable: true
                    } ) : null ) )
        }
    } ),
    withHandlers( {
        refreshFetch: ( { __updateState } ) => ( count ) => __updateState( {
            refreshIndex: count || refreshCount || 1,
            refreshable: true
        } )
    } ),
    withProps( ( { __state } ) => ( {
        [ targetPropName ]: __state[ targetPropName ],
        [ errorPropName ]: __state[ errorPropName ] } ) ),
    omitProps( [ '__state', '__updateState', '__setState' ] )
)
