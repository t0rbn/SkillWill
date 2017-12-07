function option(value, display) {
  display = display == undefined ? value : display
  return {
    value,
    display
  }
}

const all = option('', 'all')

const hamburg = option('Hamburg')

const frankfurt = option('Frankfurt')

const munich = option('München')

const berlin = option('Berlin')

const prague = option('Prag')

export const companiesFilterOptions = [
  all,
  option('S2 Germany'),
  option('S2 Swipe'),
  option('S2 Commerce'),
  option('S2 Content'),
  option('S2 AG')
]

export const locationOptionsForCompany = (company) => {
  switch(company) {
    case 'S2 Germany':
      return [all, hamburg, frankfurt, munich, prague, berlin]
    case 'S2 Swipe':
      return [all, hamburg, berlin]
    case 'S2 Commerce':
      return [all, hamburg, prague]
    case 'S2 Content':
      return [all, hamburg]
    case 'S2 AG':
      return [all, hamburg]
    default:
      return [all, hamburg, frankfurt, munich, prague, berlin]
  }
}