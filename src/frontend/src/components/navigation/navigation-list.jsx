import React from 'react'
import './navigation.less'

const NavigationList = props => (
	<ul className="navigation__list">{props.children}</ul>
)

export default NavigationList
