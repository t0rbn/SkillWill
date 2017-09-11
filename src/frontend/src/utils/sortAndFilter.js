function filterList(list, filter) {
	return list.filter(user => {
		if (filter === 'all') {
			return true
		} else {
			return user.location === filter
		}
	})
}

function sortAndFilter(list, sort, direction, filter) {
	if (direction === 'ascending') {
		list.sort((a, b) => {
			return a[sort] < b[sort] ? -1 : 1
		})
	} else {
		list.sort((a, b) => {
			return a[sort] > b[sort] ? -1 : 1
		})
	}
	const filteredList = filterList(list, filter)
	return filteredList
}

export default sortAndFilter
