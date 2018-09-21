import ajax from './ajax'

const withMessage = ( error ) => {
    try {
        const { data: payload, status } = error
        const data = ( Array.isArray( payload ) && ( payload.length > 0 ) ) ?  payload[ 0 ] : payload
        const message = `Status: ${status}${data.error ? `\nError: ${data.error}` : ''}${data.message ? `\nMessage: ${data.message.substr( 0, 100 )}` : ''}`
        return Object.assign( {}, error, { message } )
    }
    catch ( e ) {
        // eslint-disable-next-line no-console
        console.log( error )
        return Object.assign( { message: 'Unknown error. It could be that you are offline, or the server is unavailable.' }, error )
    }
}

export function sendGet( getUrl )  {
    return ajax.get( getUrl ).then(
        response => response.data,
        error =>
            Promise.reject( withMessage( error ) )
    )
}

export function sendPost( postUrl, body = {} ) {
    return ajax.post( postUrl, body ).then(
        response => response.data,
        error =>
            Promise.reject( withMessage( error ) )
    )
}

export function sendPut( putUrl, body = {} ) {
    return ajax.put( putUrl, body ).then(
        response => response.data,
        error =>
            Promise.reject( withMessage( error ) )
    )
}

export function sendDelete( getUrl )  {
    return ajax.delete( getUrl ).then(
        response => response.data,
        error =>
            Promise.reject( withMessage( error ) )
    )
}
