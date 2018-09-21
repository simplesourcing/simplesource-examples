import { branch, renderNothing }        from 'recompose'

export default ( requiredProps ) => (
    branch(
        ( props ) => ( requiredProps.some( ( propName ) => ( !props[ propName ] ) ) ),
        renderNothing
    )
)
