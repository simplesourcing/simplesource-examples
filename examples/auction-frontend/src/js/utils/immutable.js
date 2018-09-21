import equal from 'deep-equal'

export function updateObject( obj, input ) {
    const keys = Object.keys( input )
    if ( keys.length === 0 ) {
        return obj
    }
    const key = keys[ 0 ]
    const value = input[ key ]
    if ( keys.length === 1 ) {
        if ( !key ) {
            return obj
        }
        if ( !obj ) {
            return { [ key ]: value }
        }
        // if value is undefined delete it if it exists
        if ( value === undefined ) {
            const newObj = Object.assign( {}, obj )
            delete newObj[ key ]
            return newObj
        }
        const current = obj[ key ]
        if ( current && equal( current, value ) ) {
            return obj
        }
        return Object.assign( {}, obj, { [ key ]: value } )
    }
    const tail = Object.assign( {}, input )
    delete tail[ key ]
    // TODO: fold/reduce/make tail recursive
    return updateObject( updateObject( obj, { [ key ]: value } ), tail )
}
