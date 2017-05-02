import config from '../config.json'

export const SAVE_SEARCHTERMS_TO_STORE = 'SAVE_SEARCHTERMS_TO_STORE'
export function saveSearchTermsToStore(searchTerms){
	return {
		type: SAVE_SEARCHTERMS_TO_STORE,
		searchTerms
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
