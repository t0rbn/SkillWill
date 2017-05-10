import config from '../config.json'
import { browserHistory } from 'react-router'

export const ADD_SEARCH_TERMS = 'ADD_SEARCH_TERMS'
export function addSearchTerms(searchTerms){
	return {
		type: ADD_SEARCH_TERMS,
		payload: searchTerms
	}
}

export const DELETE_SEARCH_TERM = 'DELETE_SEARCH_TERM'
export function deleteSearchTerms(searchTerm){
	return {
		type: DELETE_SEARCH_TERM,
		payload: searchTerm
	}
}

export const SET_LOCATION_FILTER = 'SET_LOCATION_FILTER'
export function setLocationFilter(location){
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

export function getUserBySearchTerms(term, method){
	return function (dispatch, getState) {
		if(method === 'delete'){
			dispatch(deleteSearchTerms(term))
		} else {
			dispatch(addSearchTerms(term))
		}
		const {searchTerms} = getState()
		dispatch(fetchResults(searchTerms))
	}
}

export const FETCH_SKILL = 'FETCH_SKILL'
export function fetchSkill(searchTerms) {
	const requestURL = `${config.backendServer}/skills?search=${searchTerms}`
	const request = fetch(requestURL).then(response => response.json())
	return {
		type: FETCH_SKILL,
		payload: request
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