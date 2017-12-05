import React from 'react'
import { Link, browserHistory } from 'react-router'
import { exitSkillsEditMode } from '../../actions'
import Icon from '../icon/icon.jsx'
import { connect } from 'react-redux'

class Layer extends React.Component {
	constructor(props) {
		super(props)
		this.handleClose = this.handleClose.bind(this)
	}

	componentDidMount() {
		document.body.classList.add('layer-open')
	}

	componentWillUnmount() {
		document.body.classList.remove('layer-open')
	}

	handleClose() {
		//return to home if current page is login
		const { location } = this.props
		if (location && location.pathname.startsWith('/my-profile')) {
			browserHistory.push('/')
		} else {
			browserHistory.goBack()
		}
		document.body.classList.remove('layer-open')
		this.props.exitSkillsEditMode()
	}

	render() {
		return (
			<div className="layer-container">
				<Link onClick={this.handleClose} className="close-layer" />
				<div className="layer">
					<Link onClick={this.handleClose} className="close-btn">
						<Icon name="cross" size={22} />
					</Link>
					{this.props.children}
				</div>
			</div>
		)
	}
}

export default connect(null, { exitSkillsEditMode })(Layer)
