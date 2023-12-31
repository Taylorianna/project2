package me.svbneelmne.compose.splittrip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import me.svbneelmne.compose.splittrip.components.InputField
import me.svbneelmne.compose.splittrip.ui.theme.SplitTripTheme
import me.svbneelmne.compose.splittrip.util.calculateTotalPerPerson
import me.svbneelmne.compose.splittrip.util.calculateTotalTip
import me.svbneelmne.compose.splittrip.widgets.RoundIconButton

class MainActivity : ComponentActivity() {


    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                Column {
                    MainContent()
                }
            }
        }
    }
}


@Composable
fun MyApp(content: @Composable () -> Unit) {
    SplitTripTheme {
        Surface(color = MaterialTheme.colors.background) {
            content()
        }
    }
}

@Composable
fun Header(totalPerPerson: Double = 134.4) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp)
            .height(150.dp)
            .clip(shape = RoundedCornerShape(corner = CornerSize(12.dp))),
        //  .clip(shape = CircleShape.copy(app = CornerSize(12.dp)) can be used also
        color = Color(0xFF8D9BEE)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            val total = "%.2f".format(totalPerPerson)
            Text(
                text = "Total per Person",
                style = MaterialTheme.typography.h5
            )
            Text(
                text = "$ $total",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun MainContent() {
    val splitByState = remember {
        mutableStateOf(1)
    }
    val tipAmountState = remember {
        mutableStateOf(0.0)
    }
    val totalPerPersonState = remember {
        mutableStateOf(0.0)
    }
    BillForm(
        splitByState = splitByState,
        tipAmountState = tipAmountState,
        totalPerPersonState = totalPerPersonState
    )
}

@ExperimentalComposeUiApi
@Composable
fun BillForm(
    modifier: Modifier = Modifier,
    splitByState: MutableState<Int>,
    tipAmountState: MutableState<Double>,
    totalPerPersonState: MutableState<Double>,
    onValChange: (String) -> Unit = {}
) {
    val totalBillState = remember {
        mutableStateOf("")
    }
    val validState = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty() && totalBillState.value.trim().isDigitsOnly()
    }
    val sliderPositionState = remember {
        mutableStateOf(0f)
    }
    val tipPercent = (sliderPositionState.value * 100).toInt()


    val keyboardController = LocalSoftwareKeyboardController.current

    Header(totalPerPersonState.value)
    Surface(
        modifier = modifier
            .padding(top = 0.dp, end = 20.dp, start = 20.dp, bottom = 20.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        border = BorderStroke(width = 1.dp, color = Color.LightGray)
    ) {
        Column(
            modifier = modifier.padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            InputField(
                valueState = totalBillState,
                labelId = "Enter the bill amount",
                enabled = true,
                onAction = KeyboardActions {
                    if (!validState) return@KeyboardActions
                    onValChange(totalBillState.value.trim())
                    keyboardController?.hide()
                }
            )
            if (validState) {
                QuantitySelectionRow(modifier, splitByState)

                SplitRow(modifier, tipAmountState)

                TipSelectionRow(
                    tipPercent,
                    modifier,
                    sliderPositionState,
                    tipAmountState,
                    totalBillState,
                    totalPerPersonState,
                    splitByState
                )
            } else {
                Box {}
            }
        }

    }
}

@Composable
private fun TipSelectionRow(
    tipPercent: Int,
    modifier: Modifier,
    sliderPositionState: MutableState<Float>,
    tipAmountState: MutableState<Double>,
    totalBillState: MutableState<String>,
    totalPerPersonState: MutableState<Double>,
    splitByState: MutableState<Int>
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$tipPercent %")
        Spacer(modifier = modifier.height(14.dp))

        // Slider
        Slider(
            value = sliderPositionState.value, onValueChange = { newVal ->
                sliderPositionState.value = newVal
                tipAmountState.value =
                    calculateTotalTip(
                        totalBill = (totalBillState.value).toDouble(),
                        tipPercent
                    )
                totalPerPersonState.value = calculateTotalPerPerson(
                    totalBillState.value.toDouble(),
                    splitByState.value,
                    tipPercent
                )
            },
            modifier = modifier.padding(start = 16.dp, end = 16.dp),
            steps = 5
        )
    }
}


@Composable
private fun SplitRow(modifier: Modifier, tipAmountState: MutableState<Double>) {
    Row(
        modifier = modifier.padding(horizontal = 3.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Tip Added",
            modifier = modifier.align(alignment = Alignment.CenterVertically)
        )
        Spacer(modifier = modifier.width(200.dp))
        Text(
            text = "$ ${tipAmountState.value}",
            modifier = modifier.align(alignment = Alignment.CenterVertically)
        )
    }
}

@Composable
private fun QuantitySelectionRow(modifier: Modifier, splitValue: MutableState<Int>) {
    Row(
        modifier = modifier.padding(3.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Split",
            modifier = modifier.align(alignment = Alignment.CenterVertically)
        )
        Spacer(modifier = modifier.width(120.dp))
        Row(
            modifier = modifier.padding(horizontal = 3.dp),
            horizontalArrangement = Arrangement.End
        ) {
            RoundIconButton(
                imageVector = Icons.Default.Remove,
                onClick = {
                    splitValue.value = if (splitValue.value > 1) splitValue.value - 1 else 1
                }
            )

            Text(
                text = "${splitValue.value}",
                modifier = modifier
                    .align(alignment = Alignment.CenterVertically)
                    .padding(start = 9.dp, end = 9.dp)
            )

            RoundIconButton(
                imageVector = Icons.Default.Add,
                onClick = {
                    splitValue.value = splitValue.value + 1
                }
            )
        }
    }
}

/**
 * Default preview that will be loaded for Design
 */
@ExperimentalComposeUiApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp {
        Column {
            MainContent()
        }
    }
}