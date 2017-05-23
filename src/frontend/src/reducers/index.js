import {
	combineReducers
} from 'redux'
import {
	FETCH_RESULTS,
	FETCH_SKILLS,
	ADD_SEARCH_TERMS,
	DELETE_SEARCH_TERM,
	SET_LOCATION_FILTER,
	GET_PROFILE_DATA,
	ADD_SKILL_SEARCH,
	DELETE_SKILL_SEARCH
} from '../actions'

function searchTerms(state = [], action) {
	switch (action.type) {
		case ADD_SEARCH_TERMS:
			return state.concat(action.payload)
		case DELETE_SEARCH_TERM:
			return state.filter(searchTerm => searchTerm !== action.payload)
		default:
			return state
	}
}

function skillSearchTerms(state = [], action) {
	switch (action.type) {
		case ADD_SKILL_SEARCH:
			return action.payload
		case DELETE_SKILL_SEARCH:
			return []
		default:
			return state
	}
}

function locationFilter(state = [], action) {
	switch (action.type) {
		case SET_LOCATION_FILTER:
			return action.payload
		default:
			return state
	}
}

function fetchResultsBySearchTerms(state = [], action) {
	switch (action.type) {
		case FETCH_RESULTS:
			return {
				state,
				user: action.payload.results,
				searched: action.payload.searched
			}
		default:
			return state
	}
}

function fetchSkillsBySearchTerm(state = [], action) {
	switch (action.type) {
		case FETCH_SKILLS:
			return [...action.payload.map(skill => skill.name)]
		default:
			return state
	}
}

function getUserProfileData(state = [], action) {
	switch (action.type) {
		case GET_PROFILE_DATA:
			return Object.assign({}, state, action.payload)
		default:
			return state
	}
}

export default {
	searchTerms,
	locationFilter,
	results: fetchResultsBySearchTerms,
	user: getUserProfileData,
	skills: fetchSkillsBySearchTerm,
	skillSearchTerms
};