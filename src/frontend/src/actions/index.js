import config from '../config.json'

export const ADD_TO_URL = 'ADD_TO_URL'
function pushToURL(route){
	return {
		type: ADD_TO_URL,
		route
	}
}

export const SEARCH_TERMS = 'SEARCH_TERMS'
function searchTerms(searchTerms){
	return {
		type: SEARCH_TERMS,
		searchTerms
	}
}

export const REQUEST_RESULTS = 'REQUEST_RESULTS'
function requestResults(query){
	return {
		type: REQUEST_RESULTS,
		query
	}
}

export const RECEIVE_RESULTS = 'RECEIVE_RESULTS'
function receiveResults(data){
	console.log('data foooobaaar', data)
	return {
		type: RECEIVE_RESULTS,
		data
	}
}

export function fetchResults(searchTerms) {
	return function(dispatch) {
		dispatch(requestResults(searchTerms))
		return fetch(`${config.backendServer}/users?skills=${searchTerms}`)
			.then(r => {
				if (r.status === 400) {
					this.setState({
						results: [],
						searchItems: searchTerms,
						searchStarted: true,
						shouldUpdate: true
					})
				} else {
					r.json().then(data => {
						dispatch(receiveResults(data))
						this.setState({
							results: data,
							searchStarted: true,
							searchItems: searchTerms,
							route: `search?skills=${searchTerms}${locationString}`,
							shouldUpdate: true
						})
					})
				}
			})
			.catch(error => {
				console.error(`requestSearch:${error}`)
			})
	}
}
