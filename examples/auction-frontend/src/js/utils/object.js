import { collect } from './array'

export function map( obj, fn ) {
    return Object.assign( {}, ...Object.keys( obj ).map( key => fn( key, obj[ key ] ) ) )
}

export function filter( obj, keys ) {
    const values = collect( keys, ( key ) => {
        const value = obj[ key ]
        return ( value !== undefined ) ? { [ key ]: value } : undefined
    } )
    return Object.assign( {}, ...values )
}
