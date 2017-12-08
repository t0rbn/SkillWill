import React from 'react'
import BasicProfile from './basic-profile.jsx'
import { getUserProfileData, clearUserData } from '../../actions'
import { connect } from 'react-redux'

class OthersProfile extends React.Component {

	componentWillMount = () => {
		const userId = this.props.params.id || 'id'
		this.props.clearUserData()
		this.props.getUserProfileData(userId)
	}

	infoLayer() {
		//nothing to return
	}

	render() {
		const { user } = this.props
		return user.userLoaded ? (
			<div className="profile">
				<BasicProfile user={user} infoLayer={this.infoLayer} renderSearchedSkills={true} />
			</div>
		) : null
	}
}
function mapStateToProps(state) {
	const { user } = state
	return {
		user
	}
}

export default connect(mapStateToProps, {
	getUserProfileData,
	clearUserData
})(OthersProfile)
