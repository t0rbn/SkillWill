import React from 'react';

export default function setInitialStateFromURL(queryObject) {
	const {location, skills} = queryObject
	const dropdownLabel = location || 'Alle Standorte'
	const locationString = generateLocationToString(location)
	const searchItems = convertQueryParamsToArray(skills)

	return({
		searchItems,
		locationString,
		dropdownLabel
	})
}

function convertQueryParamsToArray(skills){
	if (skills && skills.length > 0){
		return skills.split(',')
	} else {
		return []
	}
}

function generateLocationToString(location){
	if (location){
		return `&location=${location}`
	} else {
		return ''
	}
}
