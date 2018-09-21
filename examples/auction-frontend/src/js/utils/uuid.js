export default () => 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace( /[xy]/g, function ( c ) {
    const r = Math.random() * 16 | 0 // eslint-disable-line no-bitwise, no-mixed-operators
    const v = c === 'x' ? r : ( r & 0x3 | 0x8 ) // eslint-disable-line no-bitwise, no-mixed-operators
    return v.toString( 16 )
} )
