import React from 'react'
import { Link, browserHistory } from 'react-router'
import { exitSkillsEditMode, getUserProfileData } from '../../actions'
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
		// @todo: refactor for edge cases (e.g. admin interface)
		browserHistory.push("/")
		document.body.classList.remove('layer-open')

		this.props.exitSkillsEditMode()
	}

	getuserEmailByOffsetFromCurrent(users, offset) {
		if (!users || users.length < 1) {
			return null;
		}

		const userEmails = users.map(user => user.id)
		const currentIndex = userEmails.indexOf(this.props.params.id)
		const nextIndex = currentIndex + offset

		// current user not found in list, that's an error
		if (currentIndex < 0) {
			return null
		}

		// next user for last user in list -> first user in list
		if (nextIndex >= userEmails.length) {
			return userEmails[0]
		}

		// prev user for first user in list -> last user in list
		if (nextIndex < 0) {
			return userEmails[userEmails.length -1]
		}

		const returnId = userEmails[currentIndex + offset]
		if (!returnId) {
			return null
		}
		return returnId
	}

	getNextuserEmail(users) {
		return this.getuserEmailByOffsetFromCurrent(users, 1)
	}

	getPrevuserEmail(users) {
		return this.getuserEmailByOffsetFromCurrent(users, -1)
	}

	shouldArrowsBeShown(users) {
		const inMyProfile = window.location.pathname.indexOf('my-profile') >= 0
		return !inMyProfile && (users && users.length > 1)
	}

	render() {
		const { results: { users }} = this.props
		const showArrows = this.shouldArrowsBeShown(users)
		const prevuserEmail = showArrows ? this.getPrevuserEmail(users) : ''
		const nextuserEmail = showArrows ? this.getNextuserEmail(users) : ''

		return (
			<div className="layer-container" name="containerid">
				<Link onClick={this.handleClose} className="close-layer" />

				<div className="btn-wrapper">
					<div className={showArrows ? "layer-btns" : "no-btns"}>
						<Link className="previous-arrow" to={`/profile/${prevuserEmail}`} />
						<Link className="next-arrow" to={`/profile/${nextuserEmail}`} />
					</div>

				<div className="layer">
					<Link onClick={this.handleClose} className="close-btn">
						<Icon name="cross" size={22} />
					</Link>
					{this.props.children}
				</div>

				</div>

			</div>
		)
	}
}

function mapStateToProps(state) {
	return {
		results: state.results,
		user: state.user
	}
}

export default connect(mapStateToProps, { exitSkillsEditMode, getUserProfileData })(Layer)
