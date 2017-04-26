export function pushToURL(route){
	return {
		type: 'ADD_TO_URL',
		route
	}
}

export function getPropsFromURL(query){
	return {
		type: 'GET_PROPS_FROM_URL',
		query
	}
}
