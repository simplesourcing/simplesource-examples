// Maps the given array over the given partial function and returns the mapped
// elements where f is defined
export function collect( arr, f ) {
    return arr.map( f ).filter( a => a !== undefined )
}
