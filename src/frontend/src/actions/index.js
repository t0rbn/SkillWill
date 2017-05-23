import config from '../config.json'

export const ADD_SEARCH_TERMS = 'ADD_SEARCH_TERMS'
export function addSearchTerms(searchTerms) {
	return {
		type: ADD_SEARCH_TERMS,
		payload: searchTerms
	}
}

export const DELETE_SEARCH_TERM = 'DELETE_SEARCH_TERM'
export function deleteSearchTerms(searchTerm) {
	return {
		type: DELETE_SEARCH_TERM,
		payload: searchTerm
	}
}

export const SET_LOCATION_FILTER = 'SET_LOCATION_FILTER'
export function setLocationFilter(location) {
	return {
		type: SET_LOCATION_FILTER,
		payload: location
	}
}

export const FETCH_RESULTS = 'FETCH_RESULTS'
export function fetchResults(searchTerms) {
	const requestURL = `${config.backendServer}/users?skills=${searchTerms}`
	const request = fetch(requestURL).then(response => response.json())
	return {
		type: FETCH_RESULTS,
		payload: request
	}
}

export function getUserBySearchTerms(term, method) {
	return function (dispatch, getState) {
		if (method === 'delete') {
			dispatch(deleteSearchTerms(term))
		} else {
			dispatch(addSearchTerms(term))
		}
		const {
			searchTerms
		} = getState()
		dispatch(fetchResults(searchTerms))
	}
}

export const ADD_SKILL_SEARCH = 'ADD_SKILL_SEARCH'
export function addSkillSearch(searchTerm) {
	return {
		type: ADD_SKILL_SEARCH,
		payload: searchTerm
	}
}

export const DELETE_SKILL_SEARCH = 'DELETE_SKILL_SEARCH'
export function deleteSkillSearch(searchTerm) {
	return {
		type: DELETE_SKILL_SEARCH,
		payload: searchTerm
	}
}

export const FETCH_SKILLS = 'FETCH_SKILLS'
export function fetchSkills(searchTerm) {
	const requestURL = `${config.backendServer}/skills?search=${searchTerm}`
	const request = fetch(requestURL).then(response => response.json())
	return {
		type: FETCH_SKILLS,
		payload: request
	}
}

export function getSkillBySearchTerms(term, method) {
	return function (dispatch, getState) {
		if (method === 'delete') {
			dispatch(deleteSkillSearch(term))
		} else {
			dispatch(addSkillSearch(term))
		}
		const {
			skillSearchTerms
		} = getState()
		dispatch(fetchSkills(skillSearchTerms))
	}
}

export const GET_PROFILE_DATA = 'GET_PROFILE_DATA'
export function getUserProfileData(profile) {
	const requestURL = `${config.backendServer}/users/${profile}`
	const request = fetch(requestURL).then(response => response.json())
	return {
		type: GET_PROFILE_DATA,
		payload: request
	}
}