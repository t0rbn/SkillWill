import React from 'react'
import './navigation.less'

const NavigationItem = props => {
	return <div className="navigation__item">{props.children}</div>
}

export default NavigationItem
