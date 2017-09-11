import React from 'react'
import BasicProfile from './basic-profile.jsx'
import { getUserProfileData } from '../../actions'

import { connect } from 'react-redux'

class OthersProfile extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			userId: this.props.params.id || 'id',
			dataLoaded: true,
			infoLayerAt: 0,
		}
		this.props.getUserProfileData(this.state.userId)
	}

	infoLayer() {
		//nothing to return
	}

	render() {
		return this.props.userLoaded ? (
			<div className="profile">
				<BasicProfile infoLayer={this.infoLayer} renderSearchedSkills={true} />
			</div>
		) : null
	}
}
function mapStateToProps(state) {
	return {
		userLoaded: state.user.userLoaded,
	}
}

export default connect(mapStateToProps, { getUserProfileData })(OthersProfile)
