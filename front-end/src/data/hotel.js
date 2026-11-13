/* ===== Nội dung tĩnh của khách sạn: sửa chữ tại đây ===== */

export const PHONE = { label: '0900 000 000', tel: '0900000000' }
export const ZALO_URL = 'https://zalo.me/0900000000'
export const EMAIL = 'datphong@moclanhotel.vn'
export const MAP_EMBED = 'https://maps.google.com/maps?q=My%20Khe%20Beach%20Da%20Nang&z=15&output=embed'

export const NAV_LINKS = [
  { label: 'Về khách sạn', to: '/khach-san' },
  { label: 'Phòng', to: '/#phong' },
  { label: 'Hình ảnh', to: '/#hinh-anh' },
  { label: 'Tiện ích', to: '/#tien-ich' },
]

export const HERO_SLIDES = [
  { image: 'pool', alt: 'Hồ bơi khách sạn' },
  { image: 'beach', alt: 'Bãi biển gần khách sạn' },
  { image: 'seaview', alt: 'Phòng nghỉ hướng biển' },
]

export const GUEST_OPTIONS = ['1 người lớn', '2 người lớn', '2 người lớn, 1 trẻ em', 'Gia đình 4 người']

export const FACTS = [
  { value: 48, label: 'phòng nghỉ' },
  { value: 5, suffix: ' phút', label: 'đi bộ ra biển' },
  { value: 4.8, suffix: '/5', label: 'đánh giá của khách' },
]

// span: class Tailwind để tạo bố cục lưới ảnh
export const GALLERY = [
  { image: 'balcony', alt: 'Hồ bơi', span: 'col-span-2 row-span-2' },
  { image: 'bedroom', alt: 'Phòng ngủ' },
  { image: 'bath', alt: 'Phòng tắm' },
  { image: 'restaurant', alt: 'Khu nghỉ dưỡng', span: 'md:row-span-2' },
  { image: 'dining', alt: 'Nhà hàng' },
  { image: 'beach2', alt: 'Mặt tiền khách sạn' },
]

export const AMENITIES = [
  { title: 'Hồ bơi tầng thượng', text: 'Mở cửa từ 6 giờ đến 21 giờ, có khăn tắm tại chỗ.' },
  { title: 'Bữa sáng tự chọn', text: 'Món Việt và món Âu, phục vụ từ 6 giờ 30 đến 10 giờ.' },
  { title: 'Đưa đón sân bay', text: 'Miễn phí một chiều khi đặt từ 2 đêm trở lên.' },
  { title: 'Wi-Fi tốc độ cao', text: 'Phủ sóng toàn bộ phòng và khu vực chung.' },
  { title: 'Cho thuê xe máy', text: 'Đặt tại quầy lễ tân, có mũ bảo hiểm kèm theo.' },
  { title: 'Giữ hành lý', text: 'Gửi đồ trước giờ nhận phòng hoặc sau khi trả phòng.' },
]

export const REVIEWS = [
  { quote: 'Phòng sạch, nhân viên nhiệt tình. Sáng nào cũng đi bộ ra biển ngắm bình minh rất tiện.', author: 'Chị Hà, Hà Nội' },
  { quote: 'Bữa sáng ngon, nhiều món Việt. Hồ bơi trên tầng thượng buổi tối rất mát.', author: 'Anh Minh, TP. Hồ Chí Minh' },
  { quote: 'Đi cùng hai bé, phòng gia đình rộng rãi. Lễ tân hỗ trợ đặt tour Bà Nà nhanh gọn.', author: 'Chị Thảo, Cần Thơ' },
]

export const CONTACT = [
  { label: 'Địa chỉ', value: '123 Võ Nguyên Giáp, Sơn Trà, Đà Nẵng' },
  { label: 'Điện thoại', value: PHONE.label, href: `tel:${PHONE.tel}` },
  { label: 'Email', value: EMAIL, href: `mailto:${EMAIL}` },
  { label: 'Nhận phòng', value: 'Từ 14:00' },
  { label: 'Trả phòng', value: 'Trước 12:00' },
]

/* ----- Trang giới thiệu khách sạn ----- */
export const STATS = [
  { value: '2015', label: 'năm thành lập' },
  { value: '48', label: 'phòng nghỉ' },
  { value: '400 m', label: 'đến bãi biển' },
  { value: '4,8/5', label: 'từ 1.200 đánh giá' },
]

export const PHOTO_CATEGORIES = [
  { id: 'all', label: 'Tất cả' },
  { id: 'phong', label: 'Phòng nghỉ' },
  { id: 'bien', label: 'Hồ bơi và biển' },
  { id: 'am-thuc', label: 'Ẩm thực' },
]

export const PHOTOS = [
  { image: 'pool', cat: 'bien', alt: 'Hồ bơi' },
  { image: 'seaview', cat: 'phong', alt: 'Phòng hướng biển' },
  { image: 'dining', cat: 'am-thuc', alt: 'Bữa sáng tại phòng' },
  { image: 'beach', cat: 'bien', alt: 'Bãi biển' },
  { image: 'bedroom', cat: 'phong', alt: 'Phòng ngủ' },
  { image: 'restaurant', cat: 'am-thuc', alt: 'Nhà hàng' },
  { image: 'balcony', cat: 'phong', alt: 'Ban công' },
  { image: 'resort', cat: 'bien', alt: 'Hồ bơi về đêm' },
  { image: 'bath', cat: 'phong', alt: 'Phòng tắm' },
  { image: 'family', cat: 'phong', alt: 'Phòng gia đình' },
  { image: 'beach2', cat: 'bien', alt: 'Bến tàu ra biển' },
  { image: 'roomwine', cat: 'phong', alt: 'Phòng Deluxe' },
]

export const DINING_HOURS = [
  { label: 'Bữa sáng tự chọn', value: '6:30 đến 10:00' },
  { label: 'Nhà hàng', value: '11:00 đến 22:00' },
  { label: 'Quầy bar tầng thượng', value: '16:00 đến 23:30' },
  { label: 'Phục vụ tại phòng', value: '24 giờ' },
]

export const FACILITIES = [
  { title: 'Thư giãn', items: ['Hồ bơi vô cực tầng thượng', 'Phòng tập gym mở 24 giờ', 'Spa và massage chân', 'Ghế tắm nắng và dù ở bãi biển'] },
  { title: 'Đi lại', items: ['Đưa đón sân bay', 'Cho thuê xe máy và xe đạp', 'Đặt tour Bà Nà, Hội An', 'Bãi đỗ ô tô miễn phí'] },
  { title: 'Tiện lợi', items: ['Lễ tân 24 giờ', 'Giặt ủi trong ngày', 'Giữ hành lý miễn phí', 'Phòng họp 20 chỗ'] },
]

export const NEARBY = [
  { label: 'Bãi biển Mỹ Khê', value: '400 m' },
  { label: 'Chợ đêm Sơn Trà', value: '1,5 km' },
  { label: 'Cầu Rồng', value: '3 km' },
  { label: 'Sân bay Đà Nẵng', value: '6 km' },
  { label: 'Phố cổ Hội An', value: '28 km' },
  { label: 'Bà Nà Hills', value: '30 km' },
]

export const FAQS = [
  { q: 'Tôi có thể nhận phòng sớm không?', a: 'Có, nếu còn phòng trống. Nhận phòng trước 10:00 tính thêm 50% giá một đêm, từ 10:00 đến 14:00 miễn phí tùy tình trạng phòng.' },
  { q: 'Khách sạn có cho mang thú cưng không?', a: 'Hiện tại khách sạn chưa nhận thú cưng để đảm bảo cho khách bị dị ứng.' },
  { q: 'Hủy phòng như thế nào?', a: 'Hủy miễn phí trước 3 ngày so với ngày nhận phòng. Hủy muộn hơn tính phí một đêm đầu tiên.' },
  { q: 'Có xuất hóa đơn công ty không?', a: 'Có. Bạn gửi thông tin công ty cho lễ tân khi nhận phòng, hóa đơn sẽ gửi qua email trong 3 ngày.' },
]
