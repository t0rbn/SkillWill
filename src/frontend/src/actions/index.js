import config from '../config.json'

export const ADD_TO_URL = 'ADD_TO_URL'
function pushToURL(route){
	return {
		type: ADD_TO_URL,
		route
	}
}

export const FETCH_RESULTS = 'FETCH_RESULTS'

export function fetchResults(searchTerms, locationTerm = '') {
	const requestURL = `${config.backendServer}/users?skills=${searchTerms}${locationTerm}`
	const request = fetch(requestURL).then(response => response.json())
	return {
		type: FETCH_RESULTS,
		payload: request
	}
}
export const FETCH_SKILLS = 'FETCH_SKILLS'

export function fetchSkills(searchTerms, locationTerm = '') {
	const requestURL = `${config.backendServer}/skills?search=${searchTerms}`
	const request = fetch(requestURL).then(response => response.json())
	return {
		type: FETCH_SKILLS,
		payload: request
	}
}
