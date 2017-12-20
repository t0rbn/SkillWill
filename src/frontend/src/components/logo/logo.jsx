import React from 'react'
import Icon from '../icon/icon.jsx'
import './logo.less'

const Logo = props => {
	return (
		<div className={`logo ${props.small ? 'logo--small' : ''}`}>
			<Icon className="logo__icon" name="sw-logo" />
			{!props.small && <h1 className="logo__title">skillwill</h1>}
		</div>
	)
}

export default Logo
