# CS364 - Assignment 1: BMI Calculator (ดัชนีมวลกาย)

แอปพลิเคชันสำหรับคำนวณค่าดัชนีมวลกาย (Body Mass Index : BMI) บนระบบปฏิบัติการ Android พัฒนาสำหรับรายวิชา CS364 มหาวิทยาลัยธรรมศาสตร์ อ้างอิงเกณฑ์การคำนวณและจำแนกตามองค์การอนามัยโลก (WHO) จาก [calculator.net/bmi-calculator](https://www.calculator.net/bmi-calculator.html)

---

## 👥 รายชื่อและหน้าที่ของสมาชิกในกลุ่ม

| ลำดับ | ชื่อ - นามสกุล | รหัสนักศึกษา | หน้าที่และความรับผิดชอบ |
|:---:|:---|:---:|:---|
| 1 | นาย สุทธิพจน์ สุวรรณสุทธิ์ | 6709650698 | คุมงาน (Project Manager), ควบคุมคุณภาพ (Quality Assurance & Inspection) |
| 2 | นาย ศุภวิชญ์ ไม้จัตุรัส | 6709650656 | ระบบภาษาของแอปพลิเคชัน (Localization TH/EN), ตรรกะการคำนวณ (Calculate Correctly, Calculate Again) |
| 3 | นาย วุฒิกร บุญทวี | 6709650623 | ออกแบบและจัดโครงสร้าง Layout แบบ Responsive (Portrait + Landscape) |
| 4 | นาย พลธรรม ศรีงาม | 6709650508 | ออกแบบและจัดโครงสร้าง Layout แบบ Responsive (Portrait + Landscape) |
| 5 | นาย สหรัฐ อุดมวัฒน์ทวี | 6709650680 | การตรวจสอบข้อมูลนำเข้า (Input Validation), ระดับภาวะเสี่ยง (BMI Risk Level), การปรับขนาดอักษรตามเครื่อง (Runtime Font Scale) |

---

## 📋 สรุปการปฏิบัติตามเกณฑ์ตรวจให้คะแนน (Checklist 10 ข้อ)

| # | รายการตรวจสอบ | ตำแหน่งที่ตรวจ | รายละเอียดการพัฒนา |
|:---:|:---|:---|:---|
| 1 | **Responsive Layout** | `res/layout/activity_main.xml` | จัดหน้าจอด้วย `ScrollView` และ `LinearLayout` ทำให้ส่วนแสดงผลยืด-หดได้เหมาะสมตามขนาดหน้าจอทุกอุปกรณ์ |
| 2 | **Resource ID** | `MainActivity.java` + `res/layout/` | ไม่มีจุด Hard-code สี, ข้อความ หรือขนาด ทุกค่าอ้างอิงผ่าน `R.color`, `R.string`, `R.dimen` ตามมาตรฐาน |
| 3 | **Input Validation** | `activity_main.xml` + `MainActivity.java` | ใช้ `DecimalDigitsInputFilter(8, 2)` ตามที่โจทย์กำหนด รับตัวเลขไม่เกิน 8 หลักและทศนิยมไม่เกิน 2 ตำแหน่ง ทั้งช่องน้ำหนักและส่วนสูง |
| 4 | **BMI Formatting** | `MainActivity.java` | ผลลัพธ์ BMI แสดงผลใน `TextView` (แก้ไขไม่ได้) และจัดรูปแบบทศนิยม 2 ตำแหน่งด้วย `DecimalFormat` (`#,##0.00`) |
| 5 | **BMI Risk Level** | `MainActivity.java` + `res/values/colors.xml` | แสดงระดับภาวะเสี่ยงพร้อมเปลี่ยนสีข้อความและสีพื้นหลัง Badge ตามเกณฑ์ WHO (ผอม, ปกติ, น้ำหนักเกิน, อ้วน) ผ่าน `colors.xml` |
| 6 | **Calculate Correctly** | App (Runtime) + `MainActivity.java` | คำนวณถูกต้องตามเกณฑ์ทดสอบ (เช่น 65 kg / 168 cm → 23.03 / ~23.00, 80 kg / 180 cm → 24.69 / ~24.70) |
| 7 | **Calculate Again** | App (Runtime) | รองรับการเปลี่ยนค่าน้ำหนัก/ส่วนสูงแล้วกดคำนวณซ้ำได้ทันที มีปุ่ม Reset ล้างค่าอย่างถูกต้อง |
| 8 | **Portrait + Landscape** | `layout/` + `layout-land/` | มี Layout แยกสำหรับแนวตั้งและแนวนอน (2 คอลัมน์) เหมาะสมกับหน้าจอ พร้อมเก็บ State ค่าผลลัพธ์เมื่อหมุนหน้าจอ |
| 9 | **Automatic Language** | `res/values/` + `res/values-th/` | รองรับภาษาไทยและภาษาอังกฤษ โดยเปลี่ยนอัตโนมัติตามภาษาเครื่อง (Alternative Resources) และมีปุ่มสลับภาษาในแอป |
| 10 | **Runtime Font Scale** | `MainActivity.java` + `AndroidManifest.xml` | มีตัวเลือกเปิด/ปิด (Switch) สำหรับเลือกว่าจะปรับขนาดตัวอักษรตามการตั้งค่าของเครื่อง หรือคงขนาดเดิมไว้ พร้อมรองรับการเปลี่ยนแบบ Runtime |

---

## 📱 ความต้องการของระบบและการเปิดใช้งาน
- **Min SDK**: 24 (Android 7.0 Nougat ขึ้นไป)
- **Target SDK**: 36 (Android 15+)
- **Compile SDK**: 36
- **IDE**: Android Studio Ladybug / Meerkat
