import React from 'react'

export default class SuggestionItem extends React.Component {
	constructor(props) {
		super(props)
		this.handleClick = this.handleClick.bind(this)
	}

	handleClick() {
		this.props.handleSuggestionSelected(this.props.name)
	}

	render() {
		return (
			<a className="search-suggestions-item" onClick={this.handleClick}>
				{this.props.name}
			</a>
		)
	}
}
